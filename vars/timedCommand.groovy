
@Grab('org.apache.commons:commons-lang3:3.4+')
import org.apache.commons.lang.time.StopWatch
def call(String cmdToRun) {
   def sw = new StopWatch()
   def proc = "$cmdToRun".execute()
   sw.start()
   proc.waitFor()
   sw.stop()
   println( "The process took ${(sw.getTime()/1000).toString()} seconds.\n")
}

