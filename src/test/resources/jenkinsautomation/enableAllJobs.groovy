import hudson.model.*


// For each project enable the job
for(item in Hudson.instance.items) {
  println("JOB : "+item.name)
  item.disabled=false
  item.save()
  println("\n=======\n")
}
