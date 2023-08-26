# env variables 
build image 
docker build -t hello_world_python .

## options run image and pass params -> container
* docker run -e NAME=Marko hello_world_python
* define an env variable in Dockerfile ENV NAME Rafal then no need to specify -e (when running image: docker run hello_world_python)

