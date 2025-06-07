import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://localhost:6789/api/v3/api-docs',
  serversPath: './src',
})
