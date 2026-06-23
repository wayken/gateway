export default {
  'layout': 'logicalStructure',
  'root': {
    'data': {
      'text': '根节点'
    },
    'children': [
      {
        'data': {
          'text': '二级节点',
          'generalization': {
            'text': '概要'
          }
        },
        'children': [
          {
            'data': {
              'text': '分支主题'
            },
            'children': []
          },
          {
            'data': {
              'text': '分支主题'
            },
            'children': []
          }
        ]
      }
    ]
  },
  'theme': {
    'template': 'classic4',
    'config': {
    }
  },
  'view': {
    'transform': {
      'scaleX': 1,
      'scaleY': 1
    },
    'state': {
      'scale': 1
    }
  }
}
