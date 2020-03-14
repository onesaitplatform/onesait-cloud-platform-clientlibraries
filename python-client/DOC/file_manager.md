# File Manager example
## Create FileManager
```python
import json
from onesaitplatform.files import FileManager
```

```python
HOST = "www.lab.onesaitplatform.com"
USER_TOKEN = "Bearer ..."
```

```python
manager = FileManager(host=HOST, user_token=USER_TOKEN)
manager.protocol = "https"
```

## Upload file
```python
uploaded, info = manager.upload_file("dummy_file.txt", "./dummy_file.txt")
print("Uploaded file: {}".format(uploaded))
print("Information: {}".format(info))
```

## Download file
```python
downloaded, info = manager.download_file("5ccc4b34f2df81000b8f494a")
print("Downloaded file: {}".format(downloaded))
print("Information: {}".format(info))
```
