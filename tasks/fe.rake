namespace :fe do
  
  desc "BootStraps FrontEnd project"
  task :boot do
    info "Bootstrapping FronEnd Project"
    info "Updating submodules"
    sh "git submodule init; git submodule update"
    info "Getting Deps and Building projects in Checkout folder"
    sh "cd fe ; lein deps"
    info "Setting Ruby Gems needed to ran Jasmine Spec"
    sh "sudo gem install --version '= 0.9.2.2' rake"
    sh "sudo gem install --version '= 2.9.0' rspec"
    sh "sudo gem install --version '= 0.2.5' selenium"
    sh "sudo gem install --version '= 1.1.2' jasmine"
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
