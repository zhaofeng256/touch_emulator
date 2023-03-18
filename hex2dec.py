# First, initialize the string   
testing_string = 'F'  
    
# then, Print the original string   
print ("The Hexadecimal string is: " + str(testing_string))  
    
# now, use the int() function to convert the hexadecimal string to decimal string  
convertion = int(testing_string, 16)  
    
# At last, print result  
print ("The converted hexadecimal string into decimal string is: " + str(convertion))

def hex2dec(hexstr, bits):
    value = int(hexstr, 16)
    if value & (1 << (bits - 1)):
        value -= 1 << bits
    return value

fr = open('event.txt','r')
lines = fr.readlines()
fr.close()
fw = open('convert.txt', 'w')


for line in lines:
	x = line.split()
	x1 = str(hex2dec(x[1], 32))
	x2 = str(hex2dec(x[2], 32))
	x3 = str(hex2dec(x[3], 32))
	fmt = "sendevent /dev/input/event1 {0:<3}{1:<4}{2}\n"
	ss = fmt.format(x1, x2, x3)
	print(ss, end='')
	fw.write(ss)

fw.close()