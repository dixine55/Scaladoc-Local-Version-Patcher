# ScalaDoc-Local-Version-Patcher
If you want to see your scala documentation without having to go through the pain of booting up an entire webserver, well you're in luck! This program will patch the links so you won't get those nasty CORS errors!


**How to run:**

First off generate the documentation for the project as you normally would with the command: scala-cli doc .
CD into the new scala-doc
From the folder, run the command. scala-cli run PATH/TO/scaladocPatch.scala

And voilá!
