FROM ubuntu:20.04

RUN apt-get update && apt-get install -y python

COPY hello.py .

ENTRYPOINT ["python", "hello.py"]
