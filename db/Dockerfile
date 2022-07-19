FROM mariadb:10.7

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ADD /conf.d/* /etc/mysql/conf.d/

RUN chmod 644 /etc/mysql/conf.d/*