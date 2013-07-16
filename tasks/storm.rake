namespace :storm do

  desc "Start Storm"
  task :start  do
    Rake::Task["api:config:generate"].invoke
    info ("Starting Storm")
    sh ("lein with-profile storm-local run --service storm")
  end
  
end
