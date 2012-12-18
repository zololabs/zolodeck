namespace :storm do

  desc "Start Storm"
  task :start  do
    info ("Starting Storm")
    sh ("lein run --service storm")
  end
  
end
