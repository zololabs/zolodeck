namespace :datomic do
  desc "Generate Datomic Config"
  task :config do

    bag = {:zolodeck => { :home => Dir.pwd}}

    Config.generate binding, "devops/datomic/free-transactor.properties.erb", "devops/datomic/free-transactor.properties"
    puts "Successfully Generated!!"
  end

  desc "Start Datomic"
  task :start  => :config do

    command = "cd "+ Dir.pwd + "/../zolo-repo/datomic/datomic-free-* ; ./bin/transactor " + Dir.pwd + "/devops/datomic/free-transactor.properties"
    info "Datomic is getting started"

    sh (command)
  end

  namespace :client do

    desc "Update Datomic Client jars in mvn_repo"
    task :update, :version  do |t, args|
      version = args[:version]
      info "Checking whether zolo-repo has datomic #{version} installed"
      datomic_server_dir = Dir.pwd + "/../zolo-repo/datomic/datomic-pro-#{version}"
      datomic_client_dir = Dir.pwd + "/mvn_repo/com/datomic/datomic/#{version}"

      if File.exists? datomic_server_dir
        if File.exists? datomic_client_dir
          info "Datomic Client #{version} is already installed"
        else
          info "Deleting older versions"
          cmd = "rm -rf " + Dir.pwd + "/mvn_repo/com/datomic/datomic"
          sh cmd
          info "Updating Datomic Client Libs"
          cmd = "mvn deploy:deploy-file -DgroupId=com.datomic -DartifactId=datomic -Dfile=#{datomic_server_dir}/datomic-pro-#{version}.jar -DpomFile=#{datomic_server_dir}/pom.xml -Durl=file:mvn_repo"
          sh cmd
          info "Checkin changes in mvn_repo to github"
          info "Update project.clj to dependent on datomic verion : #{version}"
        end
      else
        info <<-EOS
Update Datomic in Zolo-repo Project
   1) cd ../zolo-repo
   2) rake datomic:local:update[#{version}]
        EOS
      end

    end

  end
    
end
    


