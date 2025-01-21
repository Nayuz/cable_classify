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

# 클래스별 색상 정의
colors = ["red", "blue", "green", "yellow", "orange"]

# 신뢰도 임계값 설정
CONFIDENCE_THRESHOLD = 0.3

# 폰트 설정
try:
    font_path = "C:/Windows/Fonts/arial.ttf"  # Windows 기본 폰트 경로
    if not os.path.exists(font_path):  # 대체 경로 확인 (Linux/Ubuntu)
        font_path = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
    font = ImageFont.truetype(font_path, size=18)  # 폰트 크기를 18로 설정
except IOError:
    print("폰트를 찾을 수 없습니다. 기본 폰트를 사용합니다.")
    font = ImageFont.load_default()

# 모델 로드
print("모델 로드 중...")
model = YOLO(model_path)  # ultralytics YOLO 모델 로드
print("모델 로드 완료!")

@app.route('/', methods=['POST'])
def predict():
    try:
        # 요청에서 이미지 파일 가져오기
        if 'file' not in request.files:
            print('error here!')
            return jsonify({"error": "No image file provided"}), 400
        file = request.files['file']
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
                    color = colors[class_id % len(colors)]  # 클래스별 색상 선택

                    # 바운딩 박스 그리기
                    draw.rectangle(coords, outline=color, width=3)

                    # 텍스트 내용
                    text = f"{class_name} ({confidence:.2f})"

                    # 텍스트 크기 계산
                    text_bbox = draw.textbbox((coords[0], coords[1]), text, font=font)  # 텍스트의 경계 상자
                    text_width = text_bbox[2] - text_bbox[0]
                    text_height = text_bbox[3] - text_bbox[1]

                    # 텍스트 배경 그리기
                    text_background = [
                        (coords[0], coords[1] - text_height - 5),
                        (coords[0] + text_width, coords[1] - 5)
                    ]
                    draw.rectangle(text_background, fill="black")  # 텍스트 배경

                    # 텍스트 그리기
                    draw.text(
                        (coords[0], coords[1] - text_height - 5),
                        text,
                        fill="white",
                        font=font
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
