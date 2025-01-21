from flask import Flask, request, jsonify
from ultralytics import YOLO
from PIL import Image
import io

app = Flask(__name__)

# 모델 파일 경로 및 클래스 정의
model_path = r"models/11m_best.pt"
class_names = {
    0: "USB-A",
    1: "USB-B-mini",
    2: "USB-B-micro",
    3: "USB-C",
    4: "hdmi"
}
# 신뢰도 임계값 설정
CONFIDENCE_THRESHOLD = 0.3  # 최소 신뢰도 설정 (0.3 = 30%)

# 신뢰도 임계값 설정
CONFIDENCE_THRESHOLD = 0.3  # 최소 신뢰도 설정 (0.3 = 30%)

# 모델 로드
print("모델 로드 중...")
model = YOLO(model_path)  # ultralytics YOLO 모델 로드
print("모델 로드 완료!")

@app.route('/predict', methods=['POST'])
def predict():
    try:
        # 요청에서 이미지 파일 가져오기
        if 'image' not in request.files:
            return jsonify({"error": "No image file provided"}), 400
        file = request.files['image']
        image = Image.open(io.BytesIO(file.read()))

        # 모델 추론
        results = model(image)

        # 결과 처리
        predictions = []
        for result in results:
            for box in result.boxes:
                confidence = float(box.conf)
                if confidence >= CONFIDENCE_THRESHOLD:  # 신뢰도 임계값 필터링
                    predictions.append({
                        "class_id": int(box.cls),
                        "class_name": class_names.get(int(box.cls), "Unknown"),
                        "confidence": confidence,
                        "coordinates": box.xyxy.tolist()  # [xmin, ymin, xmax, ymax]
                    })

        return jsonify({"predictions": predictions})

    except Exception as e:
        print(f"Error during prediction: {str(e)}")
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    host = '0.0.0.0'
    port = 25565
    print(f"서버 대기 중: http://{host}:{port}")
    app.run(host=host, port=port)
