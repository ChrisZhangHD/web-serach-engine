i=0
for file in ls grep *part.txt
do
    if test -f "$file"
      then
          sort -f "$file" > "part${file%part.txt}.txt"
          file_array[$i]=part${file%part.txt}.txt
          i=$(($i + 1))
    fi
done
echo "${file_array[*]}"

