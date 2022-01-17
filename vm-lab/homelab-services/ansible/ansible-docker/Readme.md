# Create an image of the latest ansible version


```
docker build . -t ansible
sudo cp dansible /usr/local/bin/dansible
sudo chmod 755 /usr/local/bin/dansible
```