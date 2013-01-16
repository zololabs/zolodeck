namespace :ci do

  task :setup do
    info "Checkout/update zolo-repo"
    if File.exists? "#{Dir.pwd}/../zolo-repo"
      sh "cd ../zolo-repo; git pull"
    else
      sh "cd ..; git clone git@github.com:zololabs/zolo-repo.git"
    end

    Rake::Task["utils:m2clean"].invoke
    Rake::Task["api:config:generate"].invoke
  end

end

