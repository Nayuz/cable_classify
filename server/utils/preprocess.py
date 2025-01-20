from torchvision.transforms import Compose, ToTensor, Resize

def preprocess_image(image, device):
    """
    이미지를 YOLO 모델에 입력 가능한 텐서로 변환.
    
    Args:
        image (PIL.Image): 입력 이미지
        device (torch.device): 모델이 사용하는 디바이스 (CPU 또는 CUDA)
    
    Returns:
        torch.Tensor: 모델 입력에 사용 가능한 텐서
    """
    transform = Compose([Resize((640, 640)), ToTensor()])
    return transform(image).unsqueeze(0).to(device)
