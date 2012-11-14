namespace :datomic do
  desc "Generate Datomic Config"
  task :config do

    bag = {:zolodeck => { :home => Dir.pwd}}

    Config.generate binding, "devops/datomic/free-transactor.properties.erb", "devops/datomic/free-transactor.properties"
    puts "Successfully Generated!!"
  end

  desc "Start Datomic"
  task :start  => :config do

    command = "cd "+ Dir.pwd + "/devops/datomic/current ; ./bin/transactor " + Dir.pwd + "/devops/datomic/free-transactor.properties"
    info "Datomic is getting started"

    sh (command)
  end
  
end
    
