namespace :api do
  
  desc "Start API Server"
  task :server  do
    port = "4000"
    info ("Starting API server in port: " + port)
    info <<-EOS
         If you want to do incremental development. Start the server by 
            1) In Emacs
               a) Eval zolo.core
               b) (do (zolo.setup.config/setup-config)
                      (zolo.setup.datomic-setup/init-datomic)
                      (serve-headless zolo.core/app 4000))
    EOS
    sh ("lein run --service api")
  end

  namespace :config do

    desc "Generate API config for development and test"
    task :generate , :env do |t , args|

      @zolodeck_env = args[:env] || "development"
      info "Generating API config for #{@zolodeck_env} environment"

      @env = "development"
      @datomic_uri = "datomic:free://localhost:4334/zolodeck-dev"
      @kiss_api_key = "0574cc154095cc7ddcaa04480daa22903da7f1b7"

      @context_io_key = "b3h4p2id"
      @context_io_secret = "XNKjHu4bQsHeIVEz"

      @google_key = "899706640798-9h6p8ighe2ajq66mg9h8djqpd1fk6lr3.apps.googleusercontent.com"
      @google_secret = "3eTbg9vDw5Cchw8qOsZON--I"
      
      @system_properties = "{}"
      
      sh "mkdir ~/.zolo" unless File.exist?(File.expand_path("~/.zolo"))

      Config.generate binding, Dir.pwd + "/../zolo-repo/site-cookbooks/api/templates/default/zolo.clj.erb", File.expand_path("~/.zolo/zolo.clj")

      puts "Successfully Generated!!"

      info "Generating logback config"

      @graylog2_host = "monitor.zolodeck.com"
      @env = "development"

      Config.generate binding, Dir.pwd + "/../zolo-repo/site-cookbooks/api/templates/default/logback.xml.erb", File.expand_path("~/.zolo/logback.xml")

      puts "Successfully Generated!!"
      
    end
    
  end

end


