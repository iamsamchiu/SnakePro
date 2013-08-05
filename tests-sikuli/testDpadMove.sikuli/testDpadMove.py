run("adb shell am start -n com.example.android.snake/com.example.android.snake.Snake")
try:
    wait("UP.png",30)
except:
    print "[error] init fail!UP button not found!"
    raise
click("1349177874455.png")
dragDrop("RIGHT.png", "LEFT.png")
if exists("UP-6.png"):
    print "[error]UP button should NOT be found！"
    raise
print "PASS!"

    





