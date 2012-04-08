Dir['tasks/*.rake'].each { |file| load(file) }

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
