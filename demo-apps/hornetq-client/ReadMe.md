Plain old Java programm

docker rmi hornetq-client
docker build -t hornetq-client .


docker tag hornetq-client:latest alainpham/hornetq-client:latest
docker push alainpham/hornetq-client:latest


docker run -it --net primenet --ip 172.18.0.240 --rm hornetq-client
