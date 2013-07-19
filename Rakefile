Dir['tasks/*.rake'].each { |file| load(file) }

namespace :utils do

  desc "Install Lein Plugins needed for this project"
  task :lein_plugins do
    sh "lein plugin install lein-difftest 1.3.7"
    sh "lein plugin install lein-clojars 0.8.0"
  end

end

namespace :prod do

  namespace :config do

    task :download do
      output = `cd ../zolo-repo; rake api:prod:list`
      prod_api = output.split(' ')[0]
      sh  "scp #{prod_api}:/home/deployer/.zolo/zolo.clj devops/config/prod/"
    end
    
  end
end

namespace :uberjar do

  desc "For API"
  task :api do
    sh "lein clean"
    sh "lein uberjar zolo.core"
  end

  desc "For Storm. This download config from prod boxed. Pass 'debug' as option if you dont want to downlad from prod boxes"
  task :storm, :debug do |t, args|

    debug = args[:debug] || false

    sh "lein clean"
    if !debug
      if File.exists? "devops/config/prod/zolo.clj"
        sh "rm devops/config/prod/zolo.clj"
      end
      Rake::Task["prod:config:download"].invoke
    end
    sh "lein with-profile storm uberjar zolo.storm.facebook"
  end

end

namespace :test do
  
  desc "Runs API unit tests"
  task :unit do
    Rake::Task["api:config:generate"].invoke("test")
    sh "lein clean"
    info "Running API Unit Tests"
    sh "lein test"
    Rake::Task["api:config:generate"].invoke
  end
  
  desc "Runs API integration tests"
  task :integration do
    sh "lein clean"
    Rake::Task["api:config:generate"].invoke("test")
    info "Running API Integration Tests"
    info "Example : (deftest ^:integration test-upsert-user)"
    sh "lein test :integration"
    Rake::Task["api:config:generate"].invoke
  end
  
  desc "Runs API all tests"
  task :all do
    sh "lein clean"
    info "Running API Unit and Integration Tests"
    Rake::Task["api:config:generate"].invoke("test")
    sh "lein test :all"
    Rake::Task["api:config:generate"].invoke
  end

  desc "Runs API all tests using storm Lib"
  task :storm do
    sh "lein clean"
    info "Running API Unit and Integration Tests using Storm Profile"
    Rake::Task["api:config:generate"].invoke("test")
    sh "lein with-profile 1.4 test :storm"
    Rake::Task["api:config:generate"].invoke
  end
end

