FROM public.ecr.aws/docker/library/alpine:3.19.1
RUN apk --no-cache add curl
COPY run.sh /usr/local/bin/run.sh
COPY index.json /usr/local/bin/index.json
RUN chmod +x /usr/local/bin/run.sh
CMD ["run.sh"]