namespace :api do

  desc "BootStraps API project"
  task :boot do
    info "Bootstrapping API project"
    Rake::Task["utils:m2clean"].execute
    Rake::Task["api:config:generate"].execute
  end

  desc "Todos from API project"
  task :todos do
    sh "lein notes;"
  end

  desc "Getting deps for API project"
  task :deps do
    sh "lein deps; lein build-checkouts;"
  end
  
  namespace :test do

    desc "Runs API unit tests"
    task :unit do
      info "Running API Unit Tests"
      sh "lein test"
    end

    desc "Runs API integration tests"
    task :integration do
      info "Running API Integration Tests"
      info "Example : (deftest ^:integration test-upsert-user)"
      sh "lein test :integration"
    end

    desc "Runs API all tests"
    task :all do
      info "Running API Unit and Integration Tests"
      sh "lein test :all"
    end
  end

  desc "Start API Swank"
  task :swank  do
    port = "4005"
    info ("Starting API swank in port: " + port)
    info <<-EOS
         To start API server
            -  In slime
               1) Eval zolo.core
               2) (serve-headless zolo.core/app 4000)
    EOS
    sh ("; lein swank " + port)
  end

  desc "Start API Server"
  task :server  do
    port = "4000"
    info ("Starting API server in port: " + port)
    info <<-EOS
         If you want to do incremental development. Start the server by 
            1) rake api:swank
            2) In slime
               a) Eval zolo.core
               b) (serve-headless zolo.core/app 4000)
    EOS
    sh ("lein run --service api")
  end

  namespace :config do

    desc "Generate API config for development and test"
    task :generate do
      
      info "Generating API config for development and test environment"

      @zolodeck_env = "development"
      @datomic_uri = "datomic:free://localhost:4334/zolodeck-dev"
      @kiss_api_key = "0574cc154095cc7ddcaa04480daa22903da7f1b7"
      
      sh "mkdir ~/.zolo" unless File.exist?(File.expand_path("~/.zolo"))

      Config.generate binding, Dir.pwd + "/../zolo-repo/site-cookbooks/api/templates/default/zolo.clj.erb", File.expand_path("~/.zolo/zolo.clj")

      puts "Successfully Generated!!"

      info "Generating logback config"

      @graylog2_host = "monitor.zolodeck.com"
      @development = true

      Config.generate binding, Dir.pwd + "/../zolo-repo/site-cookbooks/api/templates/default/logback.xml.erb", File.expand_path("~/.zolo/logback.xml")

      puts "Successfully Generated!!"
      
    end
    
  end


end
