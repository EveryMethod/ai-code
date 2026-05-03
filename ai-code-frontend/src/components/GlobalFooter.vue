<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://www.codefather.cn"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          编程导航原创项目 by 程序员鱼皮
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: #f8fafc;
  text-align: center;
  padding: 20px;
  border-top: 1px solid #e2e8f0;
}

.copyright {
  margin: 0;
  color: #94a3b8;
  font-size: 13px;
}

.author-link {
  color: #94a3b8;
  transition: color 0.2s;
}

.author-link:hover {
  color: #1890ff;
}
</style>
