module Jasmine
  class Config

    # Add your overrides or custom config code here

  end
end

module Jasmine
  class RunAdapter
    def run(focused_suite = nil)
      jasmine_files = @jasmine_files
      css_files = @jasmine_stylesheets + (@config.css_files || [])
      js_files = @config.js_files(focused_suite)
      body = ERB.new(File.read(File.join(File.dirname(__FILE__), "run.html.erb"))).result(binding)
      [
        200,
        { 'Content-Type' => 'text/html', 'Pragma' => 'no-cache' },
        [body]
      ]
    end
  end
end