FROM ubuntu:latest
LABEL authors="hongyu"

ENTRYPOINT ["top", "-b"]