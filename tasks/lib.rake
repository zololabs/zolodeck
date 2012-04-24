namespace :lib do

  desc "Todos from API project"
  task :todos do
      sh "cd api/checkouts/demonic; lein notes"
      sh "cd api/checkouts/clj-social-lab; lein notes"
      sh "cd api/checkouts/zolo-utils; lein notes"
  end
  
  namespace :test do

    desc "Runs Lib unit tests"
    task :unit do
      info "Running Lib Unit Tests"
      sh "cd api/checkouts/demonic; lein test"
      sh "cd api/checkouts/clj-social-lab; APP_ID=\"287471481334573\" APP_SECRET=\"d3d95f16523ed39231a916aae4934d6e\" lein test"
      sh "cd api/checkouts/zolo-utils; lein test"
    end

    desc "Runs Lib integration tests"
    task :integration do
      info "Running Lib Integration Tests"
      sh "cd api/checkouts/demonic; lein test :integration"
      sh "cd api/checkouts/clj-social-lab; APP_ID=\"287471481334573\" APP_SECRET=\"d3d95f16523ed39231a916aae4934d6e\" lein test :integration"
      sh "cd api/checkouts/zolo-utils; lein test :integration"
    end

    desc "Runs Lib all tests"
    task :all => [:'lib:test:unit', :'lib:test:integration']
  end

end
