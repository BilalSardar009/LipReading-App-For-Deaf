import base64
import requests

def LipRead(filedata):
#     with open("/content/lbad6n.mpg", "rb") as f1:
#         encoded_f1 = base64.b64encode(f1.read())
#         encoded_f1=str(encoded_f1,'ascii', 'ignore')
#         filedata="data:video/mpg;base64,"+encoded_f1
    jeesoon={"name":"test.mpg","data":filedata}
    response = requests.post("https://bilalsardar-video-lipreading.hf.space/run/predict", json={
    	"data": [
    		jeesoon,
    	]
    }).json()

    data = response["data"]
    return data[0]