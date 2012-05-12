Dir['tasks/*.rake'].each { |file| load(file) }

task :default => [:'zolo:test:all']

namespace :zolo do
  
  desc "Bootstrap"
  task :boot => [:'api:boot', :'api:test:all',:'fe:boot', :'fe:test:all']

  namespace :test do
    
    desc "Run Unit tests"
    task :unit => [:'api:test:unit',  :'fe:test:unit']

    desc "Run Integration tests"
    task :integration => [:'api:test:integration',:'fe:test:integration']

    desc "Run All tests"
    task :all => [:'api:test:all', :'fe:test:all']

  end

  desc "Todos from All project"
  task :todos  => [:'api:todos']

end


namespace :utils do

  desc "Clean up Zolodeck jars from local maven repo"
  task :m2clean do
    info "Deleting Zolodeck Libs from local m2"
    sh " rm -rf ~/.m2/repository/zolodeck/"
    sh "cd api ; lein deps; lein build-checkouts;"
  end

end
