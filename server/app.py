from flask import Flask, request, jsonify
import torch
from PIL import Image
import io
import numpy as np
from utils.preprocess import preprocess_image  # 전처리 함수 import

app = Flask(__name__)

# 모델 파일 경로 및 디바이스 설정
model_path = r"models/best.pt"
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# 모델 클래스 정의
class_names = {
    0: "USB-A",
    1: "USB-B-mini",
    2: "USB-B-micro",
    3: "USB-C",
    4: "hdmi"
}

# 모델 로드
print("모델 로드 중...")
checkpoint = torch.load(model_path, map_location=device)
model = checkpoint['model'].float().eval().to(device)
print("모델 로드 완료!")

@app.after_request
def after_request(response):
    print("서버 대기 중...")
    return response

def process_predictions(detection):
    """Helper function to process each detection."""
    try:
        # 바운딩 박스 좌표
        x_center, y_center, width, height = detection[:4]

        # 객체 신뢰도
        confidence = float(detection[4])

        # 클래스 점수
        class_scores = detection[5:]
        class_id = int(np.argmax(class_scores))
        class_confidence = float(class_scores[class_id])

        print(f"Detected class_id: {class_id}, confidence: {confidence}, class_confidence: {class_confidence}")

        return {
            "xmin": float(x_center - width / 2),
            "ymin": float(y_center - height / 2),
            "xmax": float(x_center + width / 2),
            "ymax": float(y_center + height / 2),
            "confidence": confidence,
            "class_id": class_id,
            "class_name": class_names.get(class_id, "Unknown"),
            "class_confidence": class_confidence
        }
    except Exception as e:
        print(f"Error in process_predictions: {str(e)}")
        return None

@app.route('/predict', methods=['POST'])
def predict():
    if 'image' not in request.files:
        return jsonify({"error": "No image file provided"}), 400

    try:
        # 이미지 처리
        file = request.files['image']
        image = Image.open(io.BytesIO(file.read()))
        image_tensor = preprocess_image(image, device)

        # 모델 추론
        with torch.no_grad():
            results = model(image_tensor)

        # 결과 처리
        results = results[0].cpu().numpy()
        results = results[0].T  # 첫 번째 배치 선택 및 transpose

        print(f"Processing {len(results)} detections...")

        # NMS 적용
        from torchvision.ops import nms
        boxes = torch.tensor([detection[:4] for detection in results])  # 바운딩 박스 좌표
        scores = torch.tensor([detection[4] for detection in results])  # 신뢰도 점수
        indices = nms(boxes, scores, iou_threshold=0.5)  # IOU 임계값 0.5

        # NMS 결과 필터링
        filtered_results = [results[i] for i in indices]

        # 신뢰도 가장 높은 결과 선택
        best_prediction = None
        for detection in filtered_results:
            pred = process_predictions(detection)
            if pred and pred["confidence"] > 0.3:  # 임계값 적용
                if best_prediction is None or pred["confidence"] > best_prediction["confidence"]:
                    best_prediction = pred

        # **여기에 해당 코드를 삽입**
        if best_prediction:
            print(f"Best prediction: {best_prediction}")  # 서버 콘솔 출력
            return jsonify({"prediction": best_prediction})  # JSON 응답
        else:
            print("No valid predictions found")  # 서버 콘솔 출력
            return jsonify({"message": "No valid predictions found"}), 200

    except Exception as e:
        print(f"Error in predict route: {str(e)}")
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    host = '0.0.0.0'
    port = 25565
    print(f"서버 대기 중: http://{host}:{port}")
    app.run(host=host, port=port)
