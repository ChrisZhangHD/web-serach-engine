i=0
for file in ls grep *res.txt
do
    if test -f "$file"
      then
#          sort "$file" > "res${file%res.txt}.txt"
#          echo "res${file%res.txt}.txt"
          file_array[$i]=res${file%res.txt}.txt
          i=$(($i + 1))
    fi
done
echo "${file_array[*]}"
