namespace :api do

  desc "BootStraps API project"
  task :boot do
    info "Bootstrapping API project"
    info "Updating submodules"
    sh "git submodule init; git submodule sync ; git submodule update"
    info "Getting Deps and Building projects in Checkout folder"
    sh "cd api ; lein deps; lein build-checkouts;"
    sh "lein plugin install lein-difftest 1.3.7"
    sh "lein plugin install lein-notes 0.0.1"
    sh "lein plugin install lein-clojars 0.8.0"
    Rake::Task["api:config:generate"].execute
  end

  desc "Todos from API project"
  task :todos do
    sh "cd api ; lein notes;"
  end
  
  namespace :test do

    desc "Runs API unit tests"
    task :unit do
      info "Running API Unit Tests"
      sh "cd api; ZOLODECK_ENV=test lein test"
    end

    desc "Runs API integration tests"
    task :integration do
      info "Running API Integration Tests"
      info "Example : (deftest ^:integration test-upsert-user)"
      sh "cd api; ZOLODECK_ENV=test lein test :integration"
    end

    desc "Runs API all tests"
    task :all => [:'api:test:unit', :'api:test:integration']
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
    sh ("cd api; lein swank " + port)
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
    sh ("cd api; lein run")
  end

  namespace :config do

    desc "Generate API config for development and test"
    task :generate do 
      
      info "Generating API config for development and test environment"
      
      bag = {:datomic => {
          :development => {:db => 'zolodeck-dev'},
          :test => {:db => 'zolodeck-test'}}}

      bag[:datomic][:development][:db] ||= Shell.prompt("Datomic Development DB", "zolodeck-dev")
      bag[:datomic][:test][:db] ||= Shell.prompt("Datomic Test DB", "zolodeck-test")

      Config.generate binding, "api/config/zolo.clj.erb", "api/config/zolo.clj"

      puts "Successfully Generated!!"
    end
    
  end


end
