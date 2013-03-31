namespace :storm do

  desc "Start Storm"
  task :start  do
    info ("Starting Storm")
    sh ("lein with-profile storm,storm-lib run --service storm")
  end
  
end
