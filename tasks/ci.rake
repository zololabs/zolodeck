namespace :ci do

  task :run do
    info "Checkout/update zolo-repo"
    if File.exists? "#{Dir.pwd}/../zolo_repo"
      sh "cd ../zolo_repo; git update"
    else
      sh "cd ..; git clone git@github.com:zololabs/zolo-repo.git"
    end
    
  end
  
end

