from flask import Flask, request, jsonify, send_file
from ultralytics import YOLO
from PIL import Image, ImageDraw, ImageFont
import io
import os

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

        # 이미지 시각화
        draw = ImageDraw.Draw(image)
        for result in results:
            for box in result.boxes:
                confidence = float(box.conf)
                if confidence >= CONFIDENCE_THRESHOLD:  # 신뢰도 임계값 필터링
                    class_id = int(box.cls)
                    class_name = class_names.get(class_id, "Unknown")
                    coords = box.xyxy.tolist()[0]  # [xmin, ymin, xmax, ymax]

                    # 바운딩 박스와 클래스 이름 그리기
                    draw.rectangle(coords, outline="red", width=3)
                    draw.text(
                        (coords[0], coords[1] - 10),
                        f"{class_name} ({confidence:.2f})",
                        fill="red"
                    )

        # 시각화된 이미지를 바이트로 변환
        img_byte_arr = io.BytesIO()
        image.save(img_byte_arr, format='JPEG')
        img_byte_arr.seek(0)

        return send_file(img_byte_arr, mimetype='image/jpeg')

    except Exception as e:
        print(f"Error during prediction: {str(e)}")
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    host = '0.0.0.0'
    port = 5000
    print(f"서버 대기 중: http://{host}:{port}")
    app.run(host=host, port=port)