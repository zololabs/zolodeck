require 'erb'
require 'readline'

def info(message)
  puts "========================================================================================"
  puts message
  puts "========================================================================================"
end

module Shell

  def self.prompt(msg, default)
    input = Readline.readline("#{msg} [default: #{default}]>", true).chomp
    (input.nil? || "" == input.strip)  ? default : input
  end

end

module Config

  def self.generate(env, src, dest)
    cfg = ERB.new(IO.read(src)).result(env)
    File.open(dest, "w") {|f| f.write(cfg) }
  end

end


