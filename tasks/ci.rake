namespace :ci do

  task :setup do
    info "Checkout/update zolo-repo"
    if File.exists? "#{Dir.pwd}/../zolo-repo"
      sh "cd ../zolo-repo; git pull"
    else
      sh "cd ..; git clone git@github.com:zololabs/zolo-repo.git"
    end

    sh "lein -U deps"
    Rake::Task["api:config:generate"].invoke("test")
  end

end

