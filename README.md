# Scaladoc-Local-Version-Patcher
If you want to see your scala documentation without having to go through the pain of booting up an entire webserver, well you're in luck! This program will patch the links so you won't get those nasty CORS errors!


**How to run:**

You can either run it directly from the folder (1), or you can pass the directory as an argument (2).




(1)

-> First off generate the documentation for the project as you normally would with the command: 
			scala-cli doc . 

-> CD into the new scala-doc

-> From the folder, run the command:
			scala-cli run PATH/TO/scaladocPatch.scala





(2)

-> First off generate the documentation for the project as you normally would with the command: 
			scala-cli doc .

-> To patch the documentation in "C:\Users\ScalaRules\Documents\GitHub\Project1", from the folder with scaladocPatch.scala run:
			scala-cli run scaladocPatch.scala -- C:\Users\ScalaRules\Documents\GitHub\Project1

Voilá, just like that the linking should now work!
