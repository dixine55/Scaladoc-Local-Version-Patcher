//> using scala 3.3

package scaladoc_localpatch

/** Utility file to patch links generated by scaladoc to work with localhost.
*/
import java.io.{BufferedWriter, File, FileWriter}
import java.util.regex.{Matcher, Pattern}
import java.nio.file.{Files, Path, Paths}

/** To run you can either provide the directory as an argument or run the file from the project folder.
 * 
 * Example argument usage: scala-cli run scaladocPatch.scala -- C:\Users\ScalaRules\Documents\GitHub\Project1
 */
@main def run(docDir: String = System.getProperty("user.dir")): Unit = 
  val rootDirectory = new File(docDir)

  if (!rootDirectory.exists() || !rootDirectory.isDirectory) then
    println(s"Error: '$docDir' is not a valid directory.")
    sys.exit(1)

  println(s"You are about to recursively process files in: ${rootDirectory.getAbsolutePath}")
  print("Are you sure you want to continue? (y/n): ")
  val userResponse = scala.io.StdIn.readLine().trim.toLowerCase

  if (userResponse != "y") then
    println("Operation cancelled.")
    sys.exit(0)
  
  HtmlFilePatcher.processDirectory(rootDirectory)

/** HtmlFilePatcher object that handles the reading and patching of broken linking in an html file.
 */
object HtmlFilePatcher:

  /** Reads and stores the local directory*/
  val currentDir: Path = Paths.get(System.getProperty("user.dir"))

  /** Reads and stores all HTML files in local directory.*/
  val htmlFiles: Seq[File] = Files.list(currentDir)
    .toArray
    .map(_.asInstanceOf[Path])
    .filter(file => file.toString.toLowerCase.endsWith(".html") && file.getFileName.toString.toLowerCase != "index.html")
    .map(_.toFile).toIndexedSeq

  /** Takes the head from htmlFiles and converts it to a string, then proceeds to split it into smaller arrays and removes the .html part.*/
  val htmlFilterName: String = htmlFiles.headOption
    .map(file => file.toString.split("\\\\").last.dropRight(5))
    .getOrElse("")
  
  println(s"Localizing links under \\$htmlFilterName")

  /** Reads an html file
  * @param file The HTML file to read and patch.
 */
  def processHtmlFile(file: File): Unit =

    /** Loads the input from the HTML file. */
    val content: String = scala.io.Source.fromFile(file).mkString

    /** Defines the content that we want to match and save as groups.
    *
    * <a\s+                          # Match the opening <a> tag and one or more whitespace characters
    *   (?:href="([^"]+)")?          # Match the href attribute and capture its value in group 1
    *   \s*(?:t="([^"]+)")?          # Match the t attribute and capture its value in group 2
    *   \s*(?:class="([^"]+)")?      # Match the class attribute and capture its value in group 3
    *   \s*.*?>                      # Match any remaining attributes and the closing > of the <a> tag
    *   (.*?)                        # Capture the content between the opening and closing tags in group 4
    * </a>
    */
    val pattern = Pattern.compile("""<a\s+(?:href="([^"]+)")?\s*(?:t="([^"]+)")?\s*(?:class="([^"]+)")?\s*.*?>(.*?)</a>""")

    /** Using the pattern logic, the matcher finds matches fitting those criterias and group them accordingly.
    */
    val matcher = pattern.matcher(content)

    /** String buffer to rewrite the HTML code to, modifying it as we go.
    */
    val modifiedContentA = new StringBuffer()

    /** Iterating through the file until we can't find matches according to our pattern criterias
    */
    while (matcher.find()) do
      /** The href link*/
      val url = matcher.group(1)
      /** The t value*/
      val tAttribute = matcher.group(2)
      /** The class value*/
      val classAttribute = matcher.group(3)
      /** Text between opening and closing a element. ( <a> </a> )*/
      val linkText = matcher.group(4)


      // Process based on different conditions
      /** As long as url isn't empty, apply the following logic*/
      if (url != null) then
        /** If the filename contains the local directory at the end, then take the local directory.*/
        val filename = if (url.contains(s"https://scala-lang.org/api/3.x/$htmlFilterName")) new File(url).getName else url
        /** If we've opened the main html file in the top folder, change directory to one level deeper in order to keep folder structure intact.*/
        val href = if (htmlFiles.headOption.exists(_.toString == file.getAbsolutePath)) s"$htmlFilterName/$filename" else filename

        /** If we have a type attribute and the href is pointing to our local project, but on scala-lang.org, then attach an onclick event and change the link to filename.class */
        if (tAttribute != null && url.contains(s"https://scala-lang.org/api/3.x/$htmlFilterName")) then
          val modifiedUrl = s"""<a href="$href" t="$tAttribute" onclick="window.location.href this is it='$href'">$linkText</a>"""
          matcher.appendReplacement(modifiedContentA, Matcher.quoteReplacement(modifiedUrl))
          
        /** If we have a class attribute that equals documentableName, change the link to our local project. */
        else if (classAttribute != null && classAttribute == "documentableName") then
          val modifiedUrl = s"""<a href="$href" t="$tAttribute" class="$classAttribute">$linkText</a>"""
          matcher.appendReplacement(modifiedContentA, Matcher.quoteReplacement(modifiedUrl))

         /** As long as the href doesnt link to scala/Predef or oracle, add an onclick event to the local path for that filename. */
        else if (!url.contains("https://scala-lang.org/api/3.x/scala/Predef") && !url.contains("https://docs.oracle.com/")) then
          val modifiedUrl = 
            /** Checks whether the file we've opened is the main one and applies the folder structure logic from val href if true.*/
            if (htmlFiles.headOption.exists(_.toString == file.getAbsolutePath)) s"""<a href="$filename" t="$tAttribute" onclick="window.location.href='$filename'">$linkText</a>"""
            else s"""<a href="$href" onclick="window.location.href='$href'">$linkText</a>"""
          matcher.appendReplacement(modifiedContentA, Matcher.quoteReplacement(modifiedUrl))

      else
        matcher.appendReplacement(modifiedContentA, Matcher.quoteReplacement(matcher.group()))

    matcher.appendTail(modifiedContentA)

    /** Writes back the modified content to the HTML file.*/
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(modifiedContentA.toString)
    writer.close()

  /** Opens up a directory and it's subdirectiories looking for files that ends with .html and adds found ones to our files array.*/
  def processDirectory(directory: File): Unit = 
    val files: Array[File] = directory.listFiles()
    if (files != null) 
      for (file <- files) 
        if (file.isDirectory) 
          processDirectory(file)
        else if (file.getName.endsWith(".html"))
          processHtmlFile(file)