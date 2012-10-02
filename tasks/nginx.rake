namespace :nginx do

  desc "Generate Nginx Config"
  task :config do

    bag = {:zolodeck => { :home => Dir.pwd}}

    Config.generate binding, "devops/nginx/nginx.conf.erb", "devops/nginx/nginx.conf"
    Config.generate binding, "devops/nginx/sites-enabled/fe.conf.erb", "devops/nginx/sites-enabled/fe.conf"
    
    puts "Successfully Generated!!"
  end

  desc "Start nginx"
  task :start  => :config do

    command = "sudo nginx -c " + Dir.pwd + "/devops/nginx/nginx.conf"
    info "Nginx is getting started . Make sure your api server is running in port 4000"

    sh (command)
  end
  
end
    
