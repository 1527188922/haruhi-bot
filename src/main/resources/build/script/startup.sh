path=`pwd`
nohup java -jar -Xms512m -Xmx512m $path/haruhiBot-server.jar > $path/haruhiBot.log 2>&1 &
tail -f haruhiBot.log