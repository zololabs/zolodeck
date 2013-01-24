Dir['tasks/*.rake'].each { |file| load(file) }

namespace :utils do

  desc "Clean up Zolodeck jars from local maven repo"
  task :m2clean do
    info "Deleting Zolodeck Libs from local m2"
    sh " rm -rf ~/.m2/repository/zolodeck/"
    sh "lein deps"
  end

  desc "Install Lein Plugins needed for this project"
  task :lein_plugins do
    sh "lein plugin install lein-difftest 1.3.7"
    sh "lein plugin install lein-notes 0.0.1"
    sh "lein plugin install lein-clojars 0.8.0"
  end

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

