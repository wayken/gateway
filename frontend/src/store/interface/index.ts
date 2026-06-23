export interface AppState {
  theme: string;    // 主题
  device: string;   // 设备
  deviceId: string; // 设备ID
}

export interface AccountState {
  // 登录用户信息，数据结构如下：
  account: {
    name: string;           // 用户名
    acct: string;           // 用户账号
    avatar: string;         // 用户头像
    admin: boolean;         // 是否为管理员
    permissions: string[];  // 用户权限列表
  };
  session: string;  // 登录后的会话信息
}

export interface MenuState {
  isMenuCollapsed: boolean
}
