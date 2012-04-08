#Dir['tasks/*.rake'].each { |file| load(file) }

def info(message)
  puts "============================================"
  puts message
  puts "============================================"
end

task :default => [:'zolo:test:all']

namespace :zolo do
  
  desc "Bootstrap"
  task :boot => [:'api:boot', :'api:test:all', :'fe:boot', :'fe:test:all']

  namespace :test do
    
    desc "Run Unit tests"
    task :unit => [:'api:test:unit', :'fe:test:unit']

    desc "Run Integration tests"
    task :integration => [:'api:test:integration', :'fe:test:integration']

    desc "Run All tests"
    task :all => [:'api:test:all', :'fe:test:all']

  end

end

namespace :api do

  desc "BootStraps API project"
  task :boot do
    info "Bootstrapping API project"
    info "Updating submodules"
    sh "git submodule init; git submodule sync ; git submodule update"
    info "Getting Deps and Building projects in Checkout folder"
    sh "cd api ; lein deps; lein build-checkouts;"
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
      sh "cd api; ZOLODECK_ENV=test lein test :integration"
    end

    desc "Runs API all tests"
    task :all => [:'api:test:unit', :'api:test:integration']
  end

  desc "Start API Swank"
  task :swank  do
    port = "4005"
    info ("Starting API swank in port: " + port)
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
               a) (use 'ring.util.serve)
               b) Eval zolo.core
               c) (serve-headless zolo.core/app 4000)
    EOS
    sh ("cd api; lein run")
  end
end

namespace :fe do
  
  desc "BootStraps FrontEnd project"
  task :boot do
    info "Bootstrapping FronEnd Project"
    info "Updating submodules"
    sh "git submodule init; git submodule update"
    info "Getting Deps and Building projects in Checkout folder"
    sh "cd fe ; lein deps"
  end
  
  namespace :test do
    
    desc "Runs FrontEnd Unit tests"
    task :unit do
      info "Running FrontEnd Unit Tests"
      sh "cd fe/resources ; rake jasmine:ci"
    end

    desc "Runs FrontEnd Integration tests"
    task :integration => []

    desc "Runs FrontEnd all tests"
    task :all => [:'fe:test:unit', :'fe:test:integration']
    
  end

  desc "Start FrontEnd Swank"
  task :swank  do
    port = "5005"
    info ("Starting FrontEnd swank in port: " + port)
    sh ("cd fe; lein swank " + port)
  end

  desc "Start FE Server"
  task :server  do
    port = "3000"
    info ("Starting FrontEnd  server in port: " + port)
    info "Go to http://localhost:3000/web/index.html"
    sh ("cd fe; lein run")
  end

end
