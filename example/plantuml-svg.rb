#!/usr/bin/env ruby
# an example command line tool for plantuml-service
#
# usage: plantuml-svg file.plantuml (or < file.plantuml)

require 'net/http'
require 'uri'

ENDPOINT = ENV['PLANTUML_ENDPOINT'] || 'https://plantuml-service.herokuapp.com/'

if ARGV.first == "--version"
  uri = URI(ENDPOINT)
  uri.path = "/version"
  puts Net::HTTP.get(uri)
else
  uri = URI(ENDPOINT)
  uri.path = "/svg"
  request = Net::HTTP::Post.new(uri)
  request.body = ARGF.read
  Net::HTTP.start(uri.host, uri.port, use_ssl: uri.scheme == 'https') do |http|
    puts http.request(request).body
  end
end


