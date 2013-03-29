Dir['tasks/*.rake'].each { |file| load(file) }

namespace :utils do

  desc "Clean up Zolodeck jars from local maven repo"
  task :m2clean do
    info "Deleting Zolodeck Libs from local m2"
    sh " rm -rf ~/.m2/repository/zolodeck/"
    sh " rm -rf ~/.m2/repository/zololabs/"
    sh "lein deps"
  end

  desc "Install Lein Plugins needed for this project"
  task :lein_plugins do
    sh "lein plugin install lein-difftest 1.3.7"
    sh "lein plugin install lein-clojars 0.8.0"
  end

end

namespace :test do
  
  desc "Runs API unit tests"
  task :unit do
    Rake::Task["api:config:generate"].invoke("test")
    info "Running API Unit Tests"
    sh "lein test"
    Rake::Task["api:config:generate"].invoke
  end
  
  desc "Runs API integration tests"
  task :integration do
    Rake::Task["api:config:generate"].invoke("test")
    info "Running API Integration Tests"
    info "Example : (deftest ^:integration test-upsert-user)"
    sh "lein test :integration"
    Rake::Task["api:config:generate"].invoke
  end
  
  desc "Runs API all tests"
  task :all do
    info "Running API Unit and Integration Tests"
    Rake::Task["api:config:generate"].invoke("test")
    sh "lein test :all"
    Rake::Task["api:config:generate"].invoke
  end
end

