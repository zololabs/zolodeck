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
