path=`pwd`
>$path/nohup.out && nohup java -jar -Xms512m -Xmx512m $path/haruhiBot-server.jar > $path/nohup.out 2>&1 &
tail -f nohup.out