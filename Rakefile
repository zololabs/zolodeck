Dir['tasks/*.rake'].each { |file| load(file) }

namespace :utils do

  desc "Clean up Zolodeck jars from local maven repo"
  task :m2clean do
    info "Deleting Zolodeck Libs from local m2"
    sh " rm -rf ~/.m2/repository/zolodeck/"
    sh "lein deps; lein build-checkouts;"
  end

  desc "Install Lein Plugins needed for this project"
  task :lein_plugins do
    sh "lein plugin install lein-difftest 1.3.7"
    sh "lein plugin install lein-notes 0.0.1"
    sh "lein plugin install lein-clojars 0.8.0"
  end

end
