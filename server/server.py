from flask import Flask, request, jsonify, send_file
from ultralytics import YOLO
from PIL import Image, ImageDraw, ImageFont
import io
import os

app = Flask(__name__)

# 모델 파일 경로 및 클래스 정의
model_path = r"models/11m_best.pt"  # YOLO 모델 파일 경로
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
CONFIDENCE_THRESHOLD = 0.3  # 예측 결과를 표시할 최소 신뢰도

# 폰트 설정
try:
    # Windows 및 Linux/Ubuntu에서 사용할 폰트 경로 설정
    font_path = "C:/Windows/Fonts/arial.ttf"  # Windows 기본 폰트 경로
    if not os.path.exists(font_path):
        font_path = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"  # Linux 기본 폰트 경로
    font = ImageFont.truetype(font_path, size=18)  # 폰트 크기 설정
except IOError:
    print("폰트를 찾을 수 없습니다. 기본 폰트를 사용합니다.")
    font = ImageFont.load_default()

# 모델 로드
print("모델 로드 중...")
model = YOLO(model_path)  # YOLO 모델 로드
print("모델 로드 완료!")

@app.route('/predict', methods=['POST'])
def predict():
    """
    클라이언트로부터 이미지를 받아 YOLO 모델로 추론 후 시각화된 이미지를 반환합니다.
    """
    try:
        # 요청에서 이미지 파일 가져오기
        if 'image' not in request.files:
            return jsonify({"error": "이미지 파일이 제공되지 않았습니다."}), 400
        file = request.files['image']
        image = Image.open(io.BytesIO(file.read()))  # 업로드된 이미지를 PIL 이미지로 변환

        # 모델 추론 실행
        results = model(image)

        # 이미지에 바운딩 박스와 텍스트 추가
        draw = ImageDraw.Draw(image)
        for result in results:
            for box in result.boxes:
                confidence = float(box.conf)  # 예측 신뢰도
                if confidence >= CONFIDENCE_THRESHOLD:  # 신뢰도 기준 필터링
                    class_id = int(box.cls)  # 예측된 클래스 ID
                    class_name = class_names.get(class_id, "Unknown")  # 클래스 이름 가져오기
                    coords = box.xyxy.tolist()[0]  # 바운딩 박스 좌표 [xmin, ymin, xmax, ymax]
                    color = colors[class_id % len(colors)]  # 클래스별 색상 선택

                    # 바운딩 박스 그리기
                    draw.rectangle(coords, outline=color, width=3)

                    # 텍스트 내용 설정
                    text = f"{class_name} ({confidence:.2f})"

                    # 텍스트 크기 계산 및 배경 그리기
                    text_bbox = draw.textbbox((coords[0], coords[1]), text, font=font)
                    text_width = text_bbox[2] - text_bbox[0]
                    text_height = text_bbox[3] - text_bbox[1]
                    text_background = [
                        (coords[0], coords[1] - text_height - 5),
                        (coords[0] + text_width, coords[1] - 5)
                    ]
                    draw.rectangle(text_background, fill="black")  # 텍스트 배경 추가

                    # 텍스트 그리기
                    draw.text(
                        (coords[0], coords[1] - text_height - 5),
                        text,
                        fill="white",
                        font=font
                    )

        # 시각화된 이미지를 바이트로 변환하여 반환 준비
        img_byte_arr = io.BytesIO()
        image.save(img_byte_arr, format='JPEG')
        img_byte_arr.seek(0)

        return send_file(img_byte_arr, mimetype='image/jpeg')

    except Exception as e:
        # 오류 발생 시 에러 메시지 반환
        print(f"예측 중 오류 발생: {str(e)}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    # 서버 설정 및 실행
    host = '0.0.0.0'  # 모든 네트워크 인터페이스에서 접근 가능
    port = 5000  # 서버 실행 포트
    print(f"서버 실행 중: http://{host}:{port}")
    app.run(host=host, port=port)
